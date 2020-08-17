package plugin

import (
	"compass/internal/util"
	"compass/pkg/logger/fake"
	"database/sql"
	"regexp"
	"testing"
	"time"

	"github.com/DATA-DOG/go-sqlmock"
	"github.com/google/uuid"
	"github.com/jinzhu/gorm"
	"github.com/stretchr/testify/require"
	"github.com/stretchr/testify/suite"
)

type Suite struct {
	suite.Suite
	DB   *gorm.DB
	mock sqlmock.Sqlmock

	repository UseCases
	datasource *Plugin
}

func (s *Suite) SetupSuite() {
	var (
		db  *sql.DB
		err error
	)

	db, s.mock, err = sqlmock.New()
	require.NoError(s.T(), err)

	s.DB, err = gorm.Open("postgres", db)
	require.NoError(s.T(), err)

	s.DB.LogMode(true)

	fakeLogger := fake.NewLoggerFake()
	s.repository = NewMain(s.DB, fakeLogger)
}

func TestInit(t *testing.T) {
	suite.Run(t, new(Suite))
}

func (s *Suite) TestValidate() {
	plugin := Plugin{}
	var errList = plugin.Validate()

	require.NotEmpty(s.T(), errList)
}

func (s *Suite) TestFindAll() {
	var id = uuid.New()
	var timeNow = time.Now()
	var (
		baseModel = util.BaseModel{ID: id, CreatedAt: timeNow}
		name      = "test-name"
		src       = "localhost:8080"
	)

	rows := sqlmock.
		NewRows([]string{"id", "name", "src", "created_at"}).
		AddRow(id, name, src, timeNow)

	s.mock.ExpectQuery(regexp.QuoteMeta(
		`SELECT * FROM "plugins"`)).
		WillReturnRows(rows)

	res, err := s.repository.FindAll()

	expected := Plugin{
		BaseModel: baseModel,
		Name:      name,
		Src:       src,
	}

	require.NoError(s.T(), err)
	require.Contains(s.T(), res, expected)
}

func (s *Suite) TestFindById() {
	var id = uuid.New()
	var timeNow = time.Now()
	var (
		baseModel = util.BaseModel{ID: id, CreatedAt: timeNow}
		name      = "test-name"
		src       = "localhost:8080"
	)

	rows := sqlmock.
		NewRows([]string{"id", "name", "src", "created_at"}).
		AddRow(id, name, src, timeNow)

	s.mock.ExpectQuery(regexp.QuoteMeta(
		`SELECT * FROM "plugins"  WHERE (id = $1) ORDER BY "plugins"."id" ASC LIMIT 1`)).
		WithArgs(id).
		WillReturnRows(rows)

	res, err := s.repository.FindById(id.String())

	expected := Plugin{
		BaseModel: baseModel,
		Name:      name,
		Src:       src,
	}

	require.NoError(s.T(), err)
	require.Equal(s.T(), res, expected)
}

func (s *Suite) TestFindByIdError() {
	var id = uuid.New()
	var timeNow = time.Now()
	var (
		name = "test-name"
		src  = "localhost:8080"
	)

	rows := sqlmock.
		NewRows([]string{"id", "name", "src", "created_at"}).
		AddRow(id, name, src, timeNow)

	s.mock.ExpectQuery(regexp.QuoteMeta(
		`SELECT * FROM "plugins"  WHERE (id = $1) ORDER BY "plugins"."id" ASC LIMIT 1`)).
		WithArgs("1234567").
		WillReturnRows(rows)

	_, err := s.repository.FindById(id.String())

	require.Error(s.T(), err)
}

func (s *Suite) TestSavePlugin() {
	id := uuid.New()
	timeNow := time.Now()
	name := "PROMETHEUS"
	src := "localhost:8080"

	pluginStruct := Plugin{
		BaseModel: util.BaseModel{ID: id, CreatedAt: timeNow},
		Name:      name,
		Src:       src,
	}

	query := regexp.QuoteMeta(`INSERT INTO "plugins"`)

	s.mock.MatchExpectationsInOrder(false)
	s.mock.ExpectBegin()
	s.mock.ExpectQuery(query).
		WithArgs(sqlmock.AnyArg(), pluginStruct.CreatedAt, pluginStruct.Name, pluginStruct.Src).
		WillReturnRows(sqlmock.NewRows([]string{"id"}).AddRow(id))
	s.mock.ExpectCommit()

	res, err := s.repository.Save(pluginStruct)

	require.NoError(s.T(), err)
	require.Equal(s.T(), pluginStruct, res)
}
